package com.hap.shiro.common.util.RhinoSandbox;

import com.hap.shiro.common.util.RhinoSandbox.internal.RhinoSandboxImpl;

@SuppressWarnings("all")
public class RhinoSandboxes {
  public static RhinoSandbox create() {
    return new RhinoSandboxImpl();
  }
}
