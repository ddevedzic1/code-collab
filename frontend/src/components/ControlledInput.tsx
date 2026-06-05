import { useEffect, useRef, useState } from 'react';
import type { KeyboardEvent } from 'react';
import { Input, type InputProps } from '@chakra-ui/react';

interface ControlledInputProps extends Omit<InputProps, 'value' | 'onChange'> {
  defaultValue: string;
  /** Called on blur or Enter with the committed value. */
  onCommit?: (value: string) => void;
}

/**
 * Self-managing text input that resyncs when `defaultValue` changes and
 * commits its value on blur or Enter (Enter blurs the field). Mirrors the
 * controlled-input pattern from the reference project.
 */
export const ControlledInput = ({
  defaultValue,
  onCommit,
  onBlur,
  ...props
}: ControlledInputProps) => {
  const ref = useRef<HTMLInputElement>(null);
  const [value, setValue] = useState(defaultValue);

  useEffect(() => {
    setValue(defaultValue);
  }, [defaultValue]);

  const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      ref.current?.blur();
    }
  };

  return (
    <Input
      ref={ref}
      value={value}
      onChange={event => setValue(event.target.value)}
      onKeyDown={handleKeyDown}
      onBlur={event => {
        onCommit?.(value);
        onBlur?.(event);
      }}
      {...props}
    />
  );
};
