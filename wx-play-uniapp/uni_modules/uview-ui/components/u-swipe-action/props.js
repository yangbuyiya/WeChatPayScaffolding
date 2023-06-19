// yangbuyi Copyright (c) https://yby6.com 2023.

export default {
  props: {
    // 是否自动关闭其他swipe按钮组
    autoClose: {
      type: Boolean,
      default: uni.$u.props.swipeAction.autoClose,
    },
  },
};
