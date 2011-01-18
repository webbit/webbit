package webbit.async;

public interface Result<T> {
    void complete(T result);
    void error(Exception error);
}
