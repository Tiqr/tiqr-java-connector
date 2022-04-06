package tiqr.org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@AllArgsConstructor
@Getter
@NoArgsConstructor
public class MetaData implements Serializable {

    private Service service;
    private Identity identity;


}
