/*
 * FoursquareAPI - Foursquare API for Java
 * Copyright (C) 2008 - 2011 Antti Lepp� / Foyt
 * http://www.foyt.fi
 * 
 * License: 
 * 
 * Licensed under GNU Lesser General Public License Version 2.1 or later (the "LGPL") 
 * http://www.gnu.org/licenses/lgpl.html
 */

package fi.foyt.foursquare.api;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONFieldParser {
  
  public static FoursquareEntity[] parseEntities(Class<?> clazz, JSONArray jsonArray, boolean skipNonExistingFields) throws FoursquareApiException {
    FoursquareEntity[] result = (FoursquareEntity[]) Array.newInstance(clazz, jsonArray.length());
    
    for (int i = 0, l = jsonArray.length(); i < l; i++) {
      JSONObject jsonObject;
      try {
        jsonObject = jsonArray.getJSONObject(i);
        result[i] = parseEntity(clazz, jsonObject, skipNonExistingFields);
      } catch (JSONException e) {
        throw new FoursquareApiException(e);
      }
    }
    
    return result;
  }
  
  public static FoursquareEntity parseEntity(Class<?> clazz, JSONObject jsonObject, boolean skipNonExistingFields) throws FoursquareApiException {
    FoursquareEntity entity = createNewField(clazz);
    
    String[] objectFieldNames = JSONObject.getNames(jsonObject);
    if (objectFieldNames != null) {
      for (String objectFieldName : objectFieldNames) {
        Class<?> fieldClass = getFieldClass(entity.getClass(), objectFieldName);
        if (fieldClass == null) {
          Method setterMethod = getSetterMethod(entity.getClass(), objectFieldName);
          if (setterMethod == null) {
            if (!skipNonExistingFields)
              throw new FoursquareApiException("Could not find field " + objectFieldName + " from " + entity.getClass().getName() + " class");
          } else {
            Class<?>[] parameters = setterMethod.getParameterTypes();
            if (parameters.length == 1) {
              fieldClass = parameters[0];
              
              try {
                setterMethod.invoke(jsonObject, parseValue(fieldClass, jsonObject, objectFieldName, skipNonExistingFields));
              } catch (JSONException e) {
                throw new FoursquareApiException(e);
              } catch (IllegalArgumentException e) {
                throw new FoursquareApiException(e);
              } catch (IllegalAccessException e) {
                throw new FoursquareApiException(e);
              } catch (InvocationTargetException e) {
                throw new FoursquareApiException(e);
              }
            } else {
              throw new FoursquareApiException("Could not find field " + objectFieldName + " from " + entity.getClass().getName() + " class");
            }
          }
        } else {
          try {
            setEntityFieldValue(entity, objectFieldName, parseValue(fieldClass, jsonObject, objectFieldName, skipNonExistingFields));
          } catch (JSONException e) {
            throw new FoursquareApiException(e);
          }
        }
      }
    }
    
    return entity;
  }
  
  private static Object parseValue(Class<?> clazz, JSONObject jsonObject, String objectFieldName, boolean skipNonExistingFields) throws JSONException, FoursquareApiException {
    if (clazz.isArray()) {
      JSONArray jsonArray = jsonObject.getJSONArray(objectFieldName);
      Class<?> arrayClass = clazz.getComponentType();
      Object[] arrayValue = (Object[]) Array.newInstance(arrayClass, jsonArray.length());
      
      for (int i = 0, l = jsonArray.length(); i < l; i++) {
        if (arrayClass.equals(String.class)) {
          arrayValue[i] = jsonArray.getString(i);
        } else if (arrayClass.equals(Integer.class)) {
          arrayValue[i] = jsonArray.getInt(i);  
        } else if (arrayClass.equals(Long.class)) {
          arrayValue[i] = jsonArray.getLong(i);  
        } else if (arrayClass.equals(Double.class)) {
          arrayValue[i] = jsonArray.getDouble(i);  
        } else if (arrayClass.equals(Boolean.class)) {
          arrayValue[i] = jsonArray.getBoolean(i);  
        } else if (instanceOf(arrayClass, FoursquareEntity.class)) {
          arrayValue[i] = parseEntity(arrayClass, jsonArray.getJSONObject(i), skipNonExistingFields);
        } else {
          throw new FoursquareApiException("Unknown array type: " + arrayClass);
        }
      }
      
      return arrayValue;
    } else if (clazz.equals(String.class)) {
      return jsonObject.getString(objectFieldName);
    } else if (clazz.equals(Integer.class)) {
      return jsonObject.getInt(objectFieldName);
    } else if (clazz.equals(Long.class)) {
      return jsonObject.getLong(objectFieldName);
    } else if (clazz.equals(Double.class)) {
      return jsonObject.getDouble(objectFieldName);
    } else if (clazz.equals(Boolean.class)) {
      return jsonObject.getBoolean(objectFieldName);
    } else if (instanceOf(clazz, FoursquareEntity.class)) {
      return parseEntity(clazz, jsonObject.getJSONObject(objectFieldName), skipNonExistingFields); 
    } else {
      throw new FoursquareApiException("Unknown type: " + clazz);
    }
  }
  
  private static boolean instanceOf(Class<?> class1, Class<?> class2) {
    if (class1.equals(class2))
      return true;
    
    Class<?> superClass = class1.getSuperclass();
    if (!superClass.equals(Object.class)) {
      return instanceOf(superClass, class2);
    }
    
    return false;
  }
  
  private static Field getField(Class<?> entityClass, String fieldName) {
    try {
      Field field = entityClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    } catch (SecurityException e) {
      return null;
    } catch (NoSuchFieldException e) {
      Class<?> superClass = entityClass.getSuperclass();
      if (superClass.equals(Object.class))
        return null;
      else
        return getField(superClass, fieldName);
    }
  } 
  
  private static Method getMethod(Class<?> entityClass, String methodName) {
    try {
      Method method = entityClass.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method;
    } catch (SecurityException e) {
      return null;
    } catch (NoSuchMethodException e) {
      Class<?> superClass = entityClass.getSuperclass();
      if (superClass.equals(Object.class))
        return null;
      else
        return getMethod(superClass, methodName);
    }
  }
  
  private static Method getSetterMethod(Class<?> entityClass, String fieldName) {
    StringBuilder result = new StringBuilder();
    result.append("set");
    result.append(Character.toUpperCase(fieldName.charAt(0)));
    result.append(fieldName.substring(1));
    return getMethod(entityClass, result.toString());
  }
  
  private static Class<?> getFieldClass(Class<?> entityClass, String fieldName) {
    Field field = getField(entityClass, fieldName);
    return field != null ? field.getType() : null;
  } 
  
  private static void setEntityFieldValue(FoursquareEntity entity, String fieldName, Object value) {
    java.lang.reflect.Field fieldProperty;
    try {
      fieldProperty = getField(entity.getClass(), fieldName);
      fieldProperty.setAccessible(true);
      fieldProperty.set(entity, value);
    } catch (SecurityException e) {
    } catch (IllegalArgumentException e) {
    } catch (IllegalAccessException e) {
    }
  }
  
  private static FoursquareEntity createNewField(Class<?> clazz) {
    try {
      return (FoursquareEntity) clazz.newInstance();
    } catch (InstantiationException e) {
      return null;
    } catch (IllegalAccessException e) {
      return null;
    }
  }
}
