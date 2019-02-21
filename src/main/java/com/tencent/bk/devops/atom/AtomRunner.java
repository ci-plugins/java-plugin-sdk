package com.tencent.bk.devops.atom;

import com.tencent.bk.devops.atom.common.Status;
import com.tencent.bk.devops.atom.pojo.AtomBaseParam;
import com.tencent.bk.devops.atom.spi.AtomService;
import com.tencent.bk.devops.atom.spi.ServiceLoader;
import com.tencent.bk.devops.atom.spi.TaskAtom;

import java.io.IOException;

/**
 * @version 1.0
 */
@SuppressWarnings("all")
public class AtomRunner {

    public static void main(String[] args) throws IOException {
        TaskAtom atom = ServiceLoader.load(TaskAtom.class);
        AtomService annotation = atom.getClass().getAnnotation(AtomService.class);
        Class<? extends AtomBaseParam> tClass = annotation.paramClass();
        AtomContext<? extends AtomBaseParam> context = getContext(tClass);
        try {
            atom.execute(context);
        } catch (Throwable e) {
            System.err.println("Unknown Error：" + e.getMessage());
            e.printStackTrace();
            context.getResult().setStatus(Status.error);
            context.getResult().setMessage("Unknown Error：" + e.getMessage());
        } finally {
            context.persistent();
        }
    }

    private static <T extends AtomBaseParam> AtomContext<T> getContext(Class<T> tClass) throws IOException {
        return new AtomContext<>(tClass);
    }
}
