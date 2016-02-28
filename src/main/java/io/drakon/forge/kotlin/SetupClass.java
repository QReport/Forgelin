package io.drakon.forge.kotlin;

import cpw.mods.fml.relauncher.IFMLCallHook;

import java.util.Map;

/**
 * @author Emberwalker
 */
public class SetupClass implements IFMLCallHook {

    @Override
    public void injectData(Map<String, Object> data) {
        ClassLoader classLoader = (ClassLoader) data.get("classLoader");
        try {
            classLoader.loadClass("io.drakon.forge.kotlin.KotlinAdapter");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}
