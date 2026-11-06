import { useState } from 'react';
import { completePasswordReset, requestPasswordReset } from '../authApi';

export default function ForgotPassword({ token, onSwitchToLogin, onComplete }) {
	const [email, setEmail] = useState('');
	const [password, setPassword] = useState('');
	const [confirmation, setConfirmation] = useState('');
	const [message, setMessage] = useState('');
	const [error, setError] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);

	const handleSubmit = async (event) => {
		event.preventDefault();
		setError('');
		setMessage('');
		setIsSubmitting(true);

		try {
			if (token) {
				if (password !== confirmation) {
					throw new Error('The passwords do not match.');
				}
				await completePasswordReset(token, password);
				onComplete();
			} else {
				const response = await requestPasswordReset(email);
				setMessage(response.message);
			}
		} catch (requestError) {
			setError(requestError.message);
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<main className="auth-panel">
			<div className="brand"><span className="brand-mark">T</span> Test Account</div>
			<h1 className="auth-heading">{token ? 'Set a new password' : 'Reset your password'}</h1>
			<p className="auth-description">
				{token
					? 'Choose a new password for your account.'
					: 'Enter your email and we’ll send a reset link if an account exists.'}
			</p>
			{message && <p className="form-message form-message-success" role="status">{message}</p>}
			{error && <p className="form-message form-message-error" role="alert">{error}</p>}
			<form className="auth-form" onSubmit={handleSubmit}>
				{token ? (
					<>
						<div className="form-field">
							<label htmlFor="reset-password">New password</label>
							<input
								id="reset-password"
								type="password"
								autoComplete="new-password"
								minLength={8}
								maxLength={72}
								placeholder="At least 8 characters"
								value={password}
								onChange={(event) => setPassword(event.target.value)}
								required
							/>
						</div>
						<div className="form-field">
							<label htmlFor="reset-confirmation">Confirm new password</label>
							<input
								id="reset-confirmation"
								type="password"
								autoComplete="new-password"
								minLength={8}
								maxLength={72}
								placeholder="Re-enter your password"
								value={confirmation}
								onChange={(event) => setConfirmation(event.target.value)}
								required
							/>
						</div>
					</>
				) : (
					<div className="form-field">
						<label htmlFor="reset-email">Email address</label>
						<input
							id="reset-email"
							type="email"
							autoComplete="email"
							placeholder="name@example.com"
							value={email}
							onChange={(event) => setEmail(event.target.value)}
							required
						/>
					</div>
				)}
				<button className="auth-submit" type="submit" disabled={isSubmitting}>
					{isSubmitting ? 'Please wait...' : token ? 'Update password' : 'Send reset link'}
				</button>
			</form>
			<p className="auth-switch-row">
				<button className="auth-switch" type="button" onClick={onSwitchToLogin}>Back to sign in</button>
			</p>
		</main>
	);
}
