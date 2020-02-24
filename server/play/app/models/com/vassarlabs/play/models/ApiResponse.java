package models.com.vassarlabs.play.models;

import java.rmi.server.UID;

public class ApiResponse {

    private String id = (new UID()).toString();
    private UniAppResult result;

    public void setResult(Boolean success, Integer status, Object content, String message) {
        this.result = new UniAppResult(success, status, (Object)null, content, message);
    }

    public void setResult(Boolean success, Integer status, Object metadata, Object content, String message) {
        this.result = new UniAppResult(success, status, metadata, content, message);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UniAppResult getResult() {
        return result;
    }

    public void setResult(UniAppResult result) {
        this.result = result;
    }
}