package model;

import java.util.List;

public class HelloJspViewModel {
    private final List<Integer> data;
    public HelloJspViewModel(List<Integer> data) {
        this.data = data;
    }

    public List<Integer> getData() {
        return data;
    }
}
