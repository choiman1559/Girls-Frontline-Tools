package androidstub.content;

import androidstub.content.res.Resources;

/**
 * Created by Roy on 15-10-6.
 */
public class Context {
    private final Resources resources = new Resources();
    public Resources getResources() {
        return resources;
    }
    public String getPackageName() {
        return "com.bighzao.xml2axml";
    }
}