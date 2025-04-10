package net.oktawia.crazyae2addons.misc;

import net.oktawia.crazyae2addons.interfaces.ICustomNBTSerializable;

import java.util.regex.Pattern;

public class LogicSetting implements ICustomNBTSerializable {
    public String in1;
    public String in2;
    public String out;

    public LogicSetting(){
        this.in1 = "";
        this.in2 = "";
        this.out = "";
    }

    public LogicSetting(String in1, String in2, String out){
        this.in1 = in1;
        this.in2 = in2;
        this.out = out;
    }

    public String serialize(){
        return in1 + SEP + in2 + SEP + out;
    }

    @Override
    public void deserialize(String data) {
        var separated = data.split(Pattern.quote(SEP), -1);
        this.in1 = separated[0];
        this.in2 = separated[1];
        this.out = separated[2];
    }
}