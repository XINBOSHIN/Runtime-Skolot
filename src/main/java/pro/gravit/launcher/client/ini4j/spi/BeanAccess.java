/*
 * Copyright 2005,2009 Ivan SZKIBA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pro.gravit.launcher.client.ini4j.spi;

public interface BeanAccess
{
    void propAdd(String propertyName, String value);

    String propDel(String propertyName);

    String propGet(String propertyName);

    String propGet(String propertyName, int index);

    int propLength(String propertyName);

    String propSet(String propertyName, String value);

    String propSet(String propertyName, String value, int index);
}
