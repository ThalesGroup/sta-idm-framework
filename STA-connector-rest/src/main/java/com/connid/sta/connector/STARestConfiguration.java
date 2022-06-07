/**
 * Copyright (c) 2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.connid.sta.connector;

import com.evolveum.polygon.rest.AbstractRestConfiguration;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.ConfigurationProperty;
/**
 * @author semancik
 *
 */
public class STARestConfiguration extends AbstractRestConfiguration {


    private String apikey = null;

    private String tenantcode = null;

    private String group = null;

    private int pageSize = 20;


    @ConfigurationProperty(displayMessageKey = "Tenant Short code",
            helpMessageKey = "Tenant Short code")
    public String gettenantcode() {
        return tenantcode;
    }

    public void settenantcode(String tenantcode) {
        this.tenantcode = tenantcode;
    }

    @ConfigurationProperty(displayMessageKey = "STA Tenant Key",
            helpMessageKey = "STA Tenant Key")
    public String getapikey() {
        return apikey;
    }

    public void setapikey(String apikey) {
        this.apikey = apikey;
    }

    @ConfigurationProperty(displayMessageKey = "STA group Name",
            helpMessageKey = "STA Tenant Key")
    public String getgroupname() {
        return group;
    }

    public void getgroupname(String group) {
        this.group = group;
    }

    @ConfigurationProperty(displayMessageKey = "config-pageSize", helpMessageKey = "")
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }



}
