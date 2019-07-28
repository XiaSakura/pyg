package entity;

import java.io.Serializable;

/**
 * @author q9826 返回结果体 封装
 */
public class Result implements Serializable {
	private boolean success;
	private Object data;
	private String message;

	

	public Result(boolean success, Object data, String message) {
		super();
		this.success = success;
		this.data = data;
		this.message = message;
	}
	

	public Result(boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}


	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
