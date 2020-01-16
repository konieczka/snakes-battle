public class cNotification {
    private String message;
    private Integer hour;
    private Integer minute;

    public cNotification(String message, Integer hour, Integer minute) {
        this.message = message;
        this.hour = hour;
        this.minute = minute;
    }

    public  cNotification(cNotification copy){
        this.message = copy.message;
        this.hour = copy.getHour();
        this.minute = copy.getMinute();
    }

    public Integer getHour() {
        return hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public String getMessage() { return message; }

    public String display(){
        return this.message;
    }
}
