package com.coolchoice.monumentphoto.task;

/**
 * Интерфейс для передачи результата работы фоновой задачи.
 *
 */
public interface AsyncTaskCompleteListener<T> {
    public void onTaskComplete(BaseTask task, T result);
}
