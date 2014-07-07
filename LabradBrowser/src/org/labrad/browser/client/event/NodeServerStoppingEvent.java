package org.labrad.browser.client.event;

@SuppressWarnings("serial")
public class NodeServerStoppingEvent implements NodeServerEvent {
  private String node;
  private String server;
  private String instance;

  protected NodeServerStoppingEvent() {}

  public NodeServerStoppingEvent(String node, String server, String instance) {
    this.node = node;
    this.server = server;
    this.instance = instance;
  }

  public String getNode() { return node; }
  public String getServer() { return server; }
  public String getInstance() { return instance; }

  @Override
  public String toString() {
    return "node: '" + node + "', server: '" + server + "', instance: '" + instance + "'";
  }
}
