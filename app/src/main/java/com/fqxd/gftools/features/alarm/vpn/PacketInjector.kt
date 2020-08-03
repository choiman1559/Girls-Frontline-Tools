package com.fqxd.gftools.features.alarm.vpn

import android.content.Context
import android.util.Log

import com.github.megatronking.netbare.http.HttpBody
import com.github.megatronking.netbare.http.HttpRequest
import com.github.megatronking.netbare.http.HttpResponse
import com.github.megatronking.netbare.http.HttpResponseHeaderPart
import com.github.megatronking.netbare.injector.InjectorCallback
import com.github.megatronking.netbare.injector.SimpleHttpInjector
import com.github.megatronking.netbare.stream.BufferStream
import com.gitlab.prototypeg.Session
import com.gitlab.prototypeg.network.request.Request
import com.gitlab.prototypeg.network.request.RequestFactory
import com.gitlab.prototypeg.network.response.ResponseFactory

import org.apache.commons.httpclient.ChunkedInputStream
import org.apache.commons.httpclient.ChunkedOutputStream
import org.apache.commons.io.IOUtil

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class PacketInjector(private val session: Session, private val context: Context) : SimpleHttpInjector() {
    private var buffer: ByteArrayOutputStream? = null
    private var header: HttpResponseHeaderPart? = null

    private var request: Request? = null

    override fun sniffResponse(response: HttpResponse): Boolean {
        if (response.isHttps) return false
        if (response.host() == "sn-list.girlfrontline.co.kr") return false //optimize
        return true
    }

    override fun sniffRequest(request: HttpRequest): Boolean {
        if (request.isHttps) return false
        if (request.host() == "sn-list.girlfrontline.co.kr") return false //optimize
        return true
    }

    @Throws(IOException::class)
    override fun onRequestInject(request: HttpRequest, body: HttpBody, callback: InjectorCallback) {
        callback.onFinished(body)
        val str = String(body.toBuffer().array())
        Log.d("REQUEST", str)
        this.request = RequestFactory[request.path(), body.toBuffer().array()]
        session.networkManager.requestHandlerManager.handle(this.request!!)
    }

    @Throws(IOException::class)
    override fun onResponseInject(header: HttpResponseHeaderPart, callback: InjectorCallback) {
        Log.d("HEADERS", header.headers().toString())
        this.header = header
        Log.d("URL", header.uri().toString())
        callback.onFinished(header)
        buffer = ByteArrayOutputStream()
    }

    @Throws(IOException::class)
    override fun onResponseInject(httpResponse: HttpResponse, body: HttpBody, callback: InjectorCallback) {
        if ("chunked" != header!!.header("Transfer-Encoding") || "gzip" != header!!.header("Content-Encoding")) {
            Log.d("INDEX", String(body.toBuffer().array()))
            callback.onFinished(body)
            return
        }

        if (header!!.uri().path == "/index.php") {
            //server has something wrong
            callback.onFinished(body)
            return
        }

        val bytes = body.toBuffer().array()
        buffer!!.write(bytes)
        if (String(bytes).endsWith("\r\n\r\n")) {
            try {
                val inputStream = GZIPInputStream(
                        ChunkedInputStream(
                                ByteArrayInputStream(buffer!!.toByteArray())
                        )
                )
                val line = IOUtil.toByteArray(inputStream)
                inputStream.close()
                val uri = header!!.uri().path
                val newResponse: ByteArray
                if (uri!!.startsWith(session.uriHeader)) {
                    val response = ResponseFactory[
                            uri.substring(uri.length.coerceAtMost(session.uriHeader.length)),
                            line,
                            request!!
                    ]

                    Log.d("INDEX",response.toString())

                    try {
                        session.networkManager.responseHandlerManager.handle(response)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (response.isEdited) {
                        val outputStream = ByteArrayOutputStream()
                        val chunkedOutputStream = ChunkedOutputStream(outputStream)
                        val gzipOutputStream = GZIPOutputStream(chunkedOutputStream)
                        gzipOutputStream.write(response.buffer!!)
                        gzipOutputStream.finish()
                        chunkedOutputStream.finish()
                        newResponse = outputStream.toByteArray()
                        gzipOutputStream.close()
                    } else {
                        newResponse = buffer!!.toByteArray()
                    }
                } else {
                    newResponse = buffer!!.toByteArray()
                }
                buffer!!.close()
                callback.onFinished(BufferStream(ByteBuffer.wrap(newResponse)))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}
