package com.hap.shiro.JSRunner;

import com.hap.shiro.common.util.RhinoSandbox.RhinoSandbox;
import com.hap.shiro.common.util.RhinoSandbox.RhinoSandboxes;
import org.mozilla.javascript.NativeObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class JsRunner {


    @Autowired
    Multiplier multiplier;

    public JsRunner(Multiplier multiplier) {
        this.multiplier = multiplier;
    }

    public NativeObject getExecutor(String script){
        RhinoSandbox sandbox = RhinoSandboxes.create();
         sandbox = sandbox.setMaxDuration(10_000);
        Object outPut = new Object();
        try{
            outPut = sandbox.eval(script);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return (NativeObject) outPut;
    }

    private void InjectingService(RhinoSandbox sandbox){
        if (sandbox == null) {
            return;
        }
        sandbox.allow(Long.class);
        sandbox.allow(Integer.class);
        sandbox.allow(String.class);
        sandbox.inject("emagol",multiplier);
        sandbox.inject("sandbox", sandbox);
    }


}
