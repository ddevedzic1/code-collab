import { extendTheme, type ThemeConfig } from '@chakra-ui/react';

const config: ThemeConfig = {
  initialColorMode: 'dark',
  useSystemColorMode: false,
};

const colors = {
  gray: {
    900: '#1c1c1c',
    800: '#252525',
    700: '#3a3a3a',
    600: '#505050',
    500: '#666666',
    400: '#7c7c7c',
    300: '#929292',
    200: '#a7a7a7',
    100: '#d3d3d3',
  },
  textColor: {
    light: '#d5d5d5',
    medium: '#929292',
    dark: '#252525',
  },
  brand: {
    500: '#3b82f6',
    600: '#2563eb',
  },
  statusColor: {
    pending: '#929292',
    running: '#3b82f6',
    completed: '#22c55e',
    failed: '#ef4444',
  },
};

const styles = {
  global: {
    'html, body, #root': {
      height: '100%',
    },
    body: {
      bg: 'gray.900',
      color: 'textColor.light',
    },
  },
};

const components = {
  Button: {
    baseStyle: {
      fontWeight: 'normal',
      borderRadius: 'md',
    },
    defaultProps: {
      size: 'sm',
    },
  },
  Input: {
    variants: {
      outline: {
        field: {
          borderColor: 'gray.700',
          borderRadius: 'md',
          _focus: {
            borderColor: 'brand.500',
            boxShadow: 'none',
          },
          _hover: {
            borderColor: 'gray.600',
          },
        },
      },
    },
    defaultProps: {
      size: 'sm',
      variant: 'outline',
    },
  },
  Textarea: {
    variants: {
      outline: {
        borderColor: 'gray.700',
        borderRadius: 'md',
        _focus: {
          borderColor: 'brand.500',
          boxShadow: 'none',
        },
        _hover: {
          borderColor: 'gray.600',
        },
      },
    },
    defaultProps: {
      size: 'sm',
      variant: 'outline',
    },
  },
  Select: {
    variants: {
      outline: {
        field: {
          borderColor: 'gray.700',
          borderRadius: 'md',
          _focus: {
            borderColor: 'brand.500',
            boxShadow: 'none',
          },
          _hover: {
            borderColor: 'gray.600',
          },
          '> option': {
            background: 'gray.800',
          },
        },
      },
    },
    defaultProps: {
      size: 'sm',
      variant: 'outline',
    },
  },
  Tabs: {
    baseStyle: {
      tablist: {
        borderBottomWidth: '1px',
        borderColor: 'gray.700',
      },
      tab: {
        fontWeight: 'semibold',
        color: 'textColor.medium',
        _selected: { color: 'textColor.light', borderColor: 'brand.500' },
      },
    },
    defaultProps: {
      size: 'sm',
    },
  },
  Modal: {
    baseStyle: {
      dialog: {
        bg: 'gray.800',
        color: 'textColor.light',
      },
    },
  },
  Card: {
    baseStyle: {
      container: {
        bg: 'gray.800',
        borderColor: 'gray.700',
        borderWidth: '1px',
      },
    },
  },
};

const theme = extendTheme({
  config,
  styles,
  colors,
  components,
});

export default theme;
