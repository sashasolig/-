package com.coolchoice.monumentphoto.data;

import org.apache.log4j.Logger;

public interface ILogable {
    enum LogOperation {
        INSERT,
        UPDATE,
        DELETE
    }
    
    void toLog(Logger logger, LogOperation operation);
}
