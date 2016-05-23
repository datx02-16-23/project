package multiset.model;

import java.util.ArrayList;

/**
 * Created by Smith on 21/05/2016.
 */
public class RangePatterns {

    private ArrayList<Double> rangeList;

    public RangePatterns (String stringInput) {
        stringInput = stringInput.trim();

        if (stringInput.contains("-")) {
            String[] split = stringInput.split("-");
            if (split.length != 2) {
                throw new IllegalArgumentException("Missing -");
            }
            final String firstEx = split [0].replace(" ", "");
            final String secondEx = split [1].replace(" ", "");

            Double lowerRange = Double.valueOf(firstEx);
            Double UpperRange = Double.valueOf(secondEx);

            this.rangeList = new ArrayList<>();
            for (double e = lowerRange; e <= UpperRange; e++) {
                this.rangeList.add(e);
            }
        } else {
            String[] split = stringInput.split(",");

            this.rangeList = new ArrayList<>();
            for (int i = 0; i < split.length; i++) {
                this.rangeList.add(Double.valueOf(split [i].replace(" ", "")));
            }
        }
    }

    public ArrayList<Double> getList () {
        return this.rangeList;
    }
}
