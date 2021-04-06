package client;

public class Push_Up_History {
    private int _push_ups;
    private int _duration;

    public Push_Up_History(){

    }

    public Push_Up_History(int _push_ups, int _duration) {
        this._push_ups = _push_ups;
        this._duration = _duration;
    }


    public int getDuration() {
        return _duration;
    }

    public void setDuration(int _duration) {
        this._duration = _duration;
    }


    public int getPush_ups() {
        return _push_ups;
    }

    public void setPush_ups(int push_ups) {this._push_ups = push_ups;}
}
