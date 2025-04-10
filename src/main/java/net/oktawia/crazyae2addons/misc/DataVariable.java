package net.oktawia.crazyae2addons.misc;

import net.oktawia.crazyae2addons.interfaces.ICustomNBTSerializable;

import java.util.regex.Pattern;

public class DataVariable implements ICustomNBTSerializable {
    public String name;
    public Integer value;

    public DataVariable(){
        this.name = "";
        this.value = 0;
    }

    public DataVariable(String name, Integer value){
        this.name = name;
        this.value = value;
    }

    public String serialize(){
        return name + SEP + value;
    }

    @Override
    public void deserialize(String data) {
        var separated = data.split(Pattern.quote(SEP), -1);
        this.name = separated[0];
        this.value = Integer.valueOf(separated[1]);
    }
}