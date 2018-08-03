/*
 *      Copyright (C) 2014 Eugene Valchkou.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.mapping.meta;

import java.lang.reflect.Field; 
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;

/**
 * This class is a field meta information of the entity.
 */
public class EntityFieldMetaData implements Comparable {
	private static final Logger log = Logger.getLogger(EntityFieldMetaData.class.getName());
	private Field field;
	private Method getter;
	private Method setter;
	private String genericDef;
	private Class<?> collectionType;

	private DataType dataType;
	private String columnName;
	private boolean isPartition;
	private boolean isClustered;
    private int ordinal = 0;
    private SchemaBuilder.Direction sortOrder;
	private boolean isStatic;
	private boolean autoGenerate;
	
	public EntityFieldMetaData(Field field, DataType dataType, Method getter, Method setter, String columnName) {
		this.field = field;
		this.getter = getter;
		this.setter = setter;
        this.dataType = dataType;
		this.columnName = columnName;
	}
	
	public Class<?> getType() {
		return field.getType();
	}

	public DataType getDataType() {
		return dataType;
	}
	
	public String getName() {
		return field.getName();
	}
	
	/**
	 * get the value from given object using reflection on public getter method
	 * @param entity - object instance the value will be retrieved from
	 */	
	public <E> Object getValue(E entity) {
		try {
			Object ret = getter.invoke(entity, new Object[]{});
			if (field.getType().isEnum()) {
				return ((Enum<?>)ret).name();
			}
			return ret;			
		} catch (Exception e) {
			log.info("Can't get value for obj:"+entity+", method:"+getter.getName());
		}
		return null;
	}
	
	/**
	 * set the value on given object using reflection on public setter method
	 * @param entity - object instance the value will be set to
	 * @param value
	 */
	public <E> void setValue(E entity, Object value) {
		try {
			if (field.getType().isEnum()) {
				Object eval = Enum.valueOf((Class<Enum>)field.getType(), (String)value);
				setter.invoke(entity, eval);
			} else {
			    setter.invoke(entity, value);
			}
		} catch (Exception e) {
			log.info("Can't set value for obj:"+entity+", method:"+setter.getName());
		}
	}

	/**
	 * String representation of generic modifier on the field
	 * 
	 * @return generic definition
	 */
	public String getGenericDef() {
		return genericDef;
	}

	/**
	 * set column definition for the collections with generics.
	 * samples: list<text>, set<float>, map<bigint>
	 * @param genericDef generic definition
	 */
	public void setGenericDef(String genericDef) {
		this.genericDef = genericDef;
	}
	
	/**
	 * indicates if the field has generic modifier
	 * 
	 */
	public boolean isGenericType() {
		return genericDef != null;
	}

	/**
	 * get corresponding Cassandra Column name for the field
	 * @return column name
	 */
	public String getColumnName() {
		return columnName;
	}

	public boolean isPartition() {
		return isPartition;
	}

	public void setPartition(boolean isPartition) {
		this.isPartition = isPartition;
	}

	public Class<?> getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(Class<?> collectionType) {
		this.collectionType = collectionType;
	}
	
	public boolean hasCollectionType() {
		return collectionType != null;
	}

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }

    public void setAutoGenerate(boolean autoGenerate) {
        this.autoGenerate = autoGenerate;
    }

    public boolean isClustered() {
        return isClustered;
    }

    public void setClustered(boolean clustered) {
        isClustered = clustered;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public SchemaBuilder.Direction getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SchemaBuilder.Direction sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compareTo(Object o) {
        EntityFieldMetaData that = (EntityFieldMetaData)o;

        if (this.isPartition() && that.isPartition()) {
            return this.getOrdinal()-that.getOrdinal();
        }

        if (this.isPartition() && !that.isPartition()) {
            return -1;
        }

        if (that.isPartition() && !this.isPartition()) {
            return 1;
        }

        if (this.isClustered() && that.isClustered()) {
            return this.getOrdinal()-that.getOrdinal();
        }

        if (this.isClustered() && !that.isClustered()) {
            return -1;
        }

        if (that.isClustered() && !this.isClustered()) {
            return 1;
        }

        return 0;
    }


}