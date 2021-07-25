package com.fqxd.gftools.features.proxy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fqxd.gftools.global.Global;
import com.fqxd.gftools.R;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FavoriteViewAdapter extends RecyclerView.Adapter<FavoriteViewAdapter.FavoriteViewHolder> {

    JSONArray list;
    Context context;
    SharedPreferences prefs;
    OnClickListener clickListener;

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        protected MaterialCardView Layout;
        protected TextView Number;
        protected TextView Address;
        protected TextView Name;
        protected ImageButton Delete;
        protected ImageButton Edit;

        public FavoriteViewHolder(View view) {
            super(view);
            this.Name = view.findViewById(R.id.proxy_name);
            this.Layout = view.findViewById(R.id.layout);
            this.Number = view.findViewById(R.id.Number);
            this.Address = view.findViewById(R.id.proxy_address);
            this.Delete = view.findViewById(R.id.proxy_delete);
            this.Edit = view.findViewById(R.id.proxy_edit);
        }
    }

    public FavoriteViewAdapter(JSONArray list, Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(Global.Prefs, Context.MODE_PRIVATE);
        this.list = list;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_favoritesproxy, viewGroup, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        try {
            JSONObject obj = list.getJSONObject(position);
            String name = obj.getString("name");
            String port = obj.getString("port");
            String address = obj.getString("address");

            holder.Number.setText(String.valueOf(position + 1));
            holder.Address.setText(String.format("%s:%s", address, port));
            holder.Name.setText(name);

            holder.Delete.setOnClickListener(v -> {
                list.remove(position);
                prefs.edit().putString("Favorite_proxy", list.toString()).apply();
                notifyItemChanged(position);
            });

            holder.Layout.setOnClickListener(v -> clickListener.onClick(address, port, name));
            holder.Edit.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_editfavoritesproxy, null, false);
                builder.setTitle(R.string.Edit_FavoriteProxy_Title);
                builder.setView(view);

                EditText address_edit = view.findViewById(R.id.proxy_address);
                EditText port_edit = view.findViewById(R.id.proxy_port);
                EditText name_edit = view.findViewById(R.id.proxy_name);
                Button cancel = view.findViewById(R.id.proxy_cancel);
                Button submit = view.findViewById(R.id.proxy_submit);

                name_edit.setText(name);
                port_edit.setText(port);
                address_edit.setText(address);

                final AlertDialog dialog = builder.create();
                submit.setOnClickListener(v2 -> {
                    boolean Duplicate = ProxyActivity.isNameDuplicate(name_edit.getText().toString(), list);
                    if (address_edit.getText().toString().equals("") ||
                            port_edit.getText().toString().equals("") ||
                            Integer.parseInt(port_edit.getText().toString()) > 65535 ||
                            name_edit.getText().toString().equals("") ||
                            (Duplicate && !name.equals(name_edit.getText().toString()))) {

                        if (address_edit.getText().toString().equals(""))
                            address_edit.setError("Input Address");
                        if (name_edit.getText().toString().equals(""))
                            name_edit.setError("Input Name");
                        else if (Duplicate)
                            name_edit.setError("Already exists name");
                        if (port_edit.getText().toString().equals(""))
                            port_edit.setError("Input Port");
                        else if (Integer.parseInt(port_edit.getText().toString()) > 65535)
                            port_edit.setError("Limit value is 65535");

                    } else {
                        try {
                            obj.put("name", name_edit.getText().toString());
                            obj.put("address", address_edit.getText().toString());
                            obj.put("port", port_edit.getText().toString());
                            list.put(position, obj);
                            prefs.edit().putString("Favorite_proxy", list.toString()).apply();
                            notifyItemChanged(position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            dialog.dismiss();
                        }
                    }
                });

                cancel.setOnClickListener(v2 -> dialog.dismiss());
                dialog.show();
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface OnClickListener {
        void onClick(String Address, String Port, String Name);
    }

    public void setOnClickListener(OnClickListener listener) {
        clickListener = listener;
    }

    @Override
    public int getItemCount() {
        return (null != list ? list.length() : 0);
    }

}