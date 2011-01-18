package webbit.async;

public class RResult<T> implements Result<T> {

    @Override
    public void complete(T item) {
    }

    @Override
    public void error(Exception exception) {
        throw new RuntimeException(exception);
    }
}
