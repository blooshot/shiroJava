package com.hap.shiro.common.util.RhinoSandbox.internal;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.EcmaError;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("all")
public class SafeClassShutter implements ClassShutter {
  public final Set<String> allowedClasses;

  @Override
  public boolean visibleToScripts(final String fullClassName) {
    boolean _startsWith = fullClassName.startsWith("adapter");
    if (_startsWith) {
      return true;
    }
    return this.allowedClasses.contains(fullClassName);
  }

  public SafeClassShutter() {
    HashSet<String> _hashSet = new HashSet<String>();
    this.allowedClasses = _hashSet;
    String _name = EcmaError.class.getName();
    this.allowedClasses.add(_name);
  }
}
