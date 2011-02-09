package webbit.magic;

public interface Server<C extends Client> {

    void onOpen(C client) throws Exception;
    void onClose(C client) throws Exception;

}
