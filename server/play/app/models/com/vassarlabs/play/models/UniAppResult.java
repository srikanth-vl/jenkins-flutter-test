package models.com.vassarlabs.play.models;

public class UniAppResult {
	
    private Boolean success;

    private Integer status;
    
    private String message;

    private Object metadata;

    private Object content;

    public UniAppResult(Boolean success, Integer status, Object metadata, Object content, String message) {
        this.success = success;
        this.status = status;
        this.metadata = metadata;
        this.content = content;
        this.message = message;
    }

    public UniAppResult(Boolean success) {
    	 this.success = success;
	}

	public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "UniAppResult [success=" + success + ", status=" + status + ", message=" + message + ", metadata="
				+ metadata + ", content=" + content + "]";
	} 
}
