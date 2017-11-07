package me.myh.configmanage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by mayanhao on 2017/11/7.
 */
@AllArgsConstructor
@Setter
@Getter
public class DataBaseConfig implements Serializable{

    private static final long serialVersionUID = 549715860266733103L;
    private String url;
    private String userName;
    private String password;

}
