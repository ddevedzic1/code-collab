import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from './LoginForm';
import { renderWithProviders } from '../../test/renderWithProviders';

const navigateSpy = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>(
    'react-router-dom'
  );
  return { ...actual, useNavigate: () => navigateSpy };
});

beforeEach(() => {
  navigateSpy.mockClear();
});

describe('LoginForm', () => {
  it('shows validation errors and does not call login when fields are empty', async () => {
    const user = userEvent.setup();
    const login = vi.fn();

    renderWithProviders(<LoginForm />, { auth: { login } });

    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Username is required.')).toBeInTheDocument();
    expect(screen.getByText('Password is required.')).toBeInTheDocument();
    expect(login).not.toHaveBeenCalled();
    expect(navigateSpy).not.toHaveBeenCalled();
  });

  it('logs in and navigates to the dashboard on success', async () => {
    const user = userEvent.setup();
    const login = vi.fn().mockResolvedValue({ id: 'u1', username: 'alice' });

    renderWithProviders(<LoginForm />, { auth: { status: 'anon', login } });

    await user.type(screen.getByLabelText(/username/i), 'alice');
    await user.type(screen.getByLabelText(/password/i), 'secret123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() =>
      expect(login).toHaveBeenCalledWith({
        username: 'alice',
        password: 'secret123',
      })
    );
    expect(navigateSpy).toHaveBeenCalledWith('/dashboard', { replace: true });
  });

  it('keeps the user on the form when login fails', async () => {
    const user = userEvent.setup();
    const login = vi.fn().mockRejectedValue({
      code: 'unauthorized',
      message: 'Invalid credentials',
      status: 401,
      isNetwork: false,
    });

    renderWithProviders(<LoginForm />, { auth: { status: 'anon', login } });

    await user.type(screen.getByLabelText(/username/i), 'alice');
    await user.type(screen.getByLabelText(/password/i), 'wrongpass');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => expect(login).toHaveBeenCalled());
    expect(navigateSpy).not.toHaveBeenCalled();
  });
});
