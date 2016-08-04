package kolodziejczyk.olek.inzynierka.emergencyapp;

/**
 * Created by sasza_000 on 2016-08-02.
 */
public class EmergencyObject  {
    private String title, phoneNumber, message;
    private long objectId;

    public EmergencyObject(String title, String phoneNumber,String message){
        this.title=title;
        this.phoneNumber=phoneNumber;
        this.message=message;
        this.objectId=0;
    }

    public EmergencyObject(String title, String phoneNumber, String message, long objectId){
        this.title=title;
        this.phoneNumber=phoneNumber;
        this.message=message;
        this.objectId=objectId;
    }

    public String getTitle(){
        return title;
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public long getObjectId() {
        return objectId;
    }
    public String toString(){
        return "Title: "+title + " Phone Number: "+ phoneNumber;
    }

}
