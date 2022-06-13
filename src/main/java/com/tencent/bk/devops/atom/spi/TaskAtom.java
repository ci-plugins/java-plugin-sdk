package com.tencent.bk.devops.atom.spi;

import com.tencent.bk.devops.atom.AtomContext;
import com.tencent.bk.devops.atom.pojo.AtomBaseParam;

/**
 * 原子接口
 *
 * @version 1.0
 */
public interface TaskAtom<T extends AtomBaseParam> {

  /**
   * 执行原子逻辑
   *
   * @param atomContext 原子上下文
   */
  void execute(AtomContext<T> atomContext);
}
