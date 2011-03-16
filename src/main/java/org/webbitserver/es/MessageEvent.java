package org.webbitserver.es;

public class MessageEvent {
    public final String data;
    public final String lastEventId;
    public final String origin;

    public MessageEvent(String data, String lastEventId, String origin) {
        this.data = data;
        this.lastEventId = lastEventId;
        this.origin = origin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageEvent that = (MessageEvent) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (lastEventId != null ? !lastEventId.equals(that.lastEventId) : that.lastEventId != null) return false;
        if (origin != null ? !origin.equals(that.origin) : that.origin != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (lastEventId != null ? lastEventId.hashCode() : 0);
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
                "data='" + data + '\'' +
                ", lastEventId='" + lastEventId + '\'' +
                ", origin='" + origin + '\'' +
                '}';
    }
}
