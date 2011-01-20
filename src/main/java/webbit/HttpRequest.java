package webbit;

public interface HttpRequest {
    String uri();

    String header(String name);

    boolean hasHeader(String name);

    String method();
}
