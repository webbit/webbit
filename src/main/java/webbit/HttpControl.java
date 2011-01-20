package webbit;

public interface HttpControl {
    
    void nextHandler(HttpRequest request, HttpResponse response);

}
