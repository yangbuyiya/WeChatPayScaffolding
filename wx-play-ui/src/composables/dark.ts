// yangbuyi Copyright (c) https://yby6.com 2023.

import { useDark, useToggle } from "@vueuse/core";

export const isDark = useDark();
export const toggleDark = useToggle(isDark);
