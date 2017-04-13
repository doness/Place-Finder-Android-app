package gps.placefinder;

public class PlaceDetail {
    protected CharSequence value;
    protected int icon;

    PlaceDetail(int icon,CharSequence value)
    {
        this.value=value; this.icon=icon;
    }
}
