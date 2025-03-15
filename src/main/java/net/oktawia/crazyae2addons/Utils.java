package net.oktawia.crazyae2addons;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Utils {

    public static <T> List<T> rotate(List<T> inputList, int offset) {
        if (inputList.isEmpty()) {
            return new ArrayList<>(inputList); // Return empty list if input is empty
        }

        // Ensure the offset is within bounds of the list size
        int effectiveOffset = offset % inputList.size();
        if (effectiveOffset < 0) {
            effectiveOffset += inputList.size(); // Handle negative offsets
        }

        // Create a rotated list
        List<T> rotatedList = new ArrayList<>();
        rotatedList.addAll(inputList.subList(inputList.size() - effectiveOffset, inputList.size())); // End part
        rotatedList.addAll(inputList.subList(0, inputList.size() - effectiveOffset)); // Start part

        return rotatedList;
    }

    public static void asyncDelay(Runnable function, float delay) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long delayInMillis = (long) (delay * 1000);
        scheduler.schedule(() -> {
            try {
                function.run();
            } finally {
                scheduler.shutdown();
            }
        }, delayInMillis, TimeUnit.MILLISECONDS);
    }

    public static boolean checkNumber(String input) {
        boolean valid = true;
        try {
            int value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            valid = false;
        }
        return valid;
    }
    public static boolean inRange(int input, int x, int y){
        return (input >= x && input <= y);
    }

    public static String shortenNumber(double number) {
        Map<Double, String> thresholds = new LinkedHashMap<>();
        thresholds.put(1e18, "E");
        thresholds.put(1e15, "P");
        thresholds.put(1e12, "T");
        thresholds.put(1e9, "G");
        thresholds.put(1e6, "M");
        thresholds.put(1e3, "K");
        for (Map.Entry<Double, String> entry : thresholds.entrySet()) {
            double threshold = entry.getKey();
            String name = entry.getValue();
            if (number >= threshold) {
                return String.format("%.2f %s", number / threshold, name);
            }
        }
        return String.valueOf(number);
    }
}
