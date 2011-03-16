package org.webbitserver.es;

public class MessageEvent {
    public final String data;
    public final String lastEventId;

    public MessageEvent(String data, String lastEventId) {
        this.data = data;
        this.lastEventId = lastEventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageEvent that = (MessageEvent) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (lastEventId != null ? !lastEventId.equals(that.lastEventId) : that.lastEventId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (lastEventId != null ? lastEventId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
                "data='" + data + '\'' +
                ", lastEventId='" + lastEventId + '\'' +
                '}';
    }
}
