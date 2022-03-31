package tiqr.org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;


@AllArgsConstructor
@Getter
public class MetaData implements Serializable {

    private Service service;
    private Identity identity;


}
