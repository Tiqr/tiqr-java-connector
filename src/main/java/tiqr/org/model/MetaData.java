package tiqr.org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class MetaData implements Serializable {

    private Service service;
    private Identity identity;


}
