package com.coolchoice.monumentphoto.task;

import java.io.IOException;

public class ServerException  extends Exception {
	
	public ServerException(Exception innerException){
		this.innerException = innerException;
	}
	
	public ServerException(Exception innerException, int httpStatusCode){
		this.innerException = innerException;
		this.httpStatusCode = httpStatusCode;
	}
	
	private int httpStatusCode = 0;
	
	private Exception innerException;

	public Exception getInnerException() {
		return innerException;
	}

	public void setInnerException(Exception innerException) {
		this.innerException = innerException;
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	
	public boolean isIOException(){
		if(this.innerException != null && this.innerException.getClass() == IOException.class){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		String innerExceptionMessage = (this.innerException != null) ? this.innerException.getMessage() : super.toString();
	    if(this.httpStatusCode > 0){
	    	String result = "HTTPStatus = " + httpStatusCode + " " + innerExceptionMessage;
	    	return result;
	    }	    
	    return innerExceptionMessage;		
	}

}
