package pl.tajchert.smartmirror.api;


import com.bluelinelabs.logansquare.LoganSquare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;


public class LoganSquareConverter implements Converter {
    private boolean modifyOutput;

    public LoganSquareConverter(boolean modifyOutput) {
        this.modifyOutput = modifyOutput;
    }

    @Override public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            // Check if the type contains a parametrized list
            String input = toString(body.in());
            if(input.startsWith("[")) {
                input = "{\"storyIds\":" + input + "}";
            }
            if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                // Grab the actual type parameter from the parametrized list and delegate to LoganSquare
                ParameterizedType parameterized = (ParameterizedType) type;

                return LoganSquare.parseList(input, (Class) parameterized.getActualTypeArguments()[0]);

            } else {
                // Single elements get parsed immediately
                return LoganSquare.parse(input, (Class) type);
            }
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    private String toString(InputStream inputStream) {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }

    @SuppressWarnings("unchecked") @Override public TypedOutput toBody(Object object) {
        try {
            // Check if the type contains a parametrized list
            if (List.class.isAssignableFrom(object.getClass())) {
                // Convert the input to a list first, access the first element and serialize the list
                List<Object> list = (List<Object>) object;
                if (list.isEmpty()) {
                    return new TypedString("[]");
                } else {
                    Object firstElement = list.get(0);
                    return new TypedString(LoganSquare.serialize(list, (Class<Object>) firstElement.getClass()));
                }
            } else {
                // Serialize single elements immediately
                return new TypedString(LoganSquare.serialize(object));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}