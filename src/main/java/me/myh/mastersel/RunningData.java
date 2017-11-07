package me.myh.mastersel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author mayanhao
 * Created by mayanhao on 2017/11/5.
 */

@Getter
@Setter
public class RunningData implements Serializable{


    private static final long serialVersionUID = -1218641067715923095L;

    private int cid;
    private String name;

}
