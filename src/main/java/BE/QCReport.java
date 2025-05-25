package BE;

import java.time.LocalDateTime;

public class QCReport {

    private int id;
    private int orderId;
    private LocalDateTime time;
    private User user;
    private boolean sent;

    public QCReport() {
    }

    public QCReport(int orderId, LocalDateTime now, User sender, boolean isSent) {
        this.orderId = orderId;
        this.time = now;
        this.user = sender;
        this.sent = isSent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
