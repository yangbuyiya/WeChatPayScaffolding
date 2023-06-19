// yangbuyi Copyright (c) https://yby6.com 2023.

export default {
  props: {
    // 用于滚动到指定item
    anchor: {
      type: [String, Number],
      default: uni.$u.props.listItem.anchor,
    },
  },
};
