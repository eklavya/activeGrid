package com.imaginea.activegrid.core.utils

import java.lang.reflect.Field
import java.util.Date

import com.imaginea.activegrid.core.models.BaseEntity
import com.imaginea.activegrid.core.utils.ReflectionUtils.PropertyType.PropertyType

/**
  * Created by babjik on 28/9/16.
  */
object ReflectionUtils {


  /**
    * Returns the type of the field
    * @param entity
    * @param fieldName
    * @tparam T
    * @return
    */
  def getType[T <: BaseEntity: Manifest] (entity: T, fieldName: String) : Class[_] = {
    val field: Field = entity.getClass.getField(fieldName)
    field.getType
  }

  /**
    * Returns the value of the field from the entity
    * @param entity
    * @param field
    * @tparam T
    * @return
    */
  def getValue[T <: BaseEntity] (entity: T, field: Field): Any  = {
    field.setAccessible(true)
    field.get(entity)
  }

  /**
    * Sets the given value to the entity for given field
    * @param entity
    * @param field
    * @param value
    * @tparam T
    * @return
    */
  def setValue[T <: BaseEntity: Manifest] (entity: T, field: Field, value: Any) : Unit = {
    field.setAccessible(true)
    field.set(entity, value)
  }

  /**
    * Checks for the field type, if the filed is a primitive returns true, else returns false
    * @param fieldType
    * @return
    */
  def isSimpleType(fieldType: Class[_]) : Boolean = {
    if (fieldType.isPrimitive || fieldType.isAssignableFrom(classOf[String]) ||
        fieldType.isAssignableFrom(classOf[Long]) || fieldType.isAssignableFrom(classOf[Double]) ||
        fieldType.isAssignableFrom(classOf[Int]) || fieldType.isAssignableFrom(classOf[Integer]) ||
        fieldType.isAssignableFrom(classOf[Float]) || fieldType.isAssignableFrom(classOf[Char]) ||
        fieldType.isAssignableFrom(classOf[Character]) || fieldType.isAssignableFrom(classOf[Byte]) ||
        fieldType.isAssignableFrom(classOf[Short]) || fieldType.isAssignableFrom(classOf[Boolean])
    ) {
        return true
    }
    false
  }

  /**
    * Checks whether the field is extend from the BaseEntity  or not
    * @param fieldType
    * @return
    */
  def isEntityType(fieldType: Class[_]): Boolean = {
    classOf[BaseEntity].isAssignableFrom(fieldType)
  }

  /**
    * Checks whether the field is a map type
    * @param fieldType
    * @return
    */
  def isMapType(fieldType: Class[_]): Boolean = {
    classOf[Map[_,_]].isAssignableFrom(fieldType)
  }

  /**
    * Checks whether the field is of type array or not
    * @param fieldType
    * @return
    */
  def isArrayType(fieldType: Class[_]): Boolean = {
    fieldType.isArray
  }

  /**
    * Checks for the field, whether it is a type of List or Set
    * @param fieldType
    * @return
    */
  def isCollectionType(fieldType: Class[_]): Boolean = {
    classOf[List[_]].isAssignableFrom(fieldType) || classOf[Set[_]].isAssignableFrom(fieldType)
  }

  /**
    * Checks whether the field is Enumeration type or not
    * @param fieldType
    * @return
    */
  def isEnum(fieldType: Class[_]): Boolean = {
    classOf[Enumeration].isAssignableFrom(fieldType)
  }

  /**
    * Checks whether the field is date type
    * @param fieldType
    * @return
    */
  def isDateType(fieldType: Class[_]): Boolean = {
    classOf[Date].isAssignableFrom(fieldType)
  }


  /**
    * Returns the Type of filed,
    * @param fieldType
    * @return  returns the Property type enumeration
    */
  def getPropertyType(fieldType: Class[_]): PropertyType = {
    if (isSimpleType(fieldType)) {
      return PropertyType.SIMPLE
    } else if (isEntityType(fieldType)) {
      return PropertyType.ENTITY
    } else if (isEnum(fieldType)) {
      return PropertyType.ENUM
    } else if (isCollectionType(fieldType)) {
      return PropertyType.COLLECTION
    } else if (isArrayType(fieldType)) {
      return PropertyType.ARRAY
    } else if (isMapType(fieldType)) {
      return PropertyType.MAP
    } else if (isDateType(fieldType)) {
      return PropertyType.DATE
    }

    PropertyType.COMPLEX
  }

  /**
    * Defines the different types of fields need to handled
    */
  object PropertyType extends Enumeration {
    type PropertyType = Value
    val SIMPLE, ARRAY, COLLECTION, MAP, ENTITY, COMPLEX, ENUM, DATE = Value
  }
}
