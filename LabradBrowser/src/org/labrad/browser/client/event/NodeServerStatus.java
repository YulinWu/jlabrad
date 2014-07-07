package org.labrad.browser.client.event;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class NodeServerStatus implements Serializable {
  private String name;
  private String description;
  private String version;
  private String instanceName;
  private List<String> environmentVars;
  private List<String> instances;

  public NodeServerStatus() {}

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public void setEnvironmentVars(List<String> environmentVars) {
    this.environmentVars = environmentVars;
  }

  public void setInstances(List<String> instances) {
    this.instances = instances;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getVersion() {
    return version;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public List<String> getEnvironmentVars() {
    return environmentVars;
  }

  public List<String> getInstances() {
    return instances;
  }
}
