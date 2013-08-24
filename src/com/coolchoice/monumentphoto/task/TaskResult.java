package com.coolchoice.monumentphoto.task;

/**
 * Результат работы фоновой задачи
 */
public class TaskResult {
	public enum  Status {SERVER_UNAVALAIBLE, LOGIN_FAILED, LOGIN_SUCCESSED, HANDLE_ERROR, CANCEL_TASK};
	
	private boolean error = false;
	private String errorText;
	private String result;
	private String taskName;
	private Status status;	
	
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	/**
	 * @return the hasError
	 */
	public boolean isError() {
		return error;
	}
	/**
	 * @param hasError to set
	 */
	public void setError(boolean isError) {
		this.error = isError;
	}
	/**
	 * @return the errorText
	 */
	public String getErrorText() {
		return errorText;
	}
	/**
	 * @param errorText to set
	 */
	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}
	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}
	/**
	 * @param result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}
	/**
	 * @return the taskName
	 */
	public String getTaskName() {
		return taskName;
	}
	/**
	 * @param taskName to set
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
