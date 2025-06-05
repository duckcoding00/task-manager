import { goto, invalidateAll } from '$app/navigation';

export interface EnhanceOptions {
	onSuccess?: (result?: any) => void | Promise<void>;
	onRedirect?: (location?: string) => void | Promise<void>;
	onError?: (errorMessage?: string, errorMessages?: string[]) => void;
	onLoading?: (loading?: boolean) => void;

	customErrorHandler?: (result?: any) => {
		message: string;
		messages: string[];
	} | null;

	autoRedirect?: boolean;
	autoInvalidate?: boolean;
	logResults?: boolean;

	successMessage?: string;
	onSuccessMessage?: (message: string) => void;
}

export const formEnhance = (options: EnhanceOptions = {}) => {
	const {
		onSuccess,
		onRedirect,
		onError,
		onLoading,
		customErrorHandler,
		autoRedirect = true,
		autoInvalidate = true,
		logResults = true,
		successMessage,
		onSuccessMessage
	} = options;

	return () => {
		return async ({ result, update }: any) => {
			if (onLoading) onLoading(false);

			if (logResults) console.log('form result:', result);

			// redirect
			if (result.type === 'redirect') {
				if (onRedirect) {
					await onRedirect(result.location);
				}
				if (autoInvalidate) {
					await invalidateAll();
				}
				if (autoRedirect) {
					goto(result.location);
				}

				return;
			}

			// success
			if (result.type === 'success') {
				if (successMessage && onSuccessMessage) {
					onSuccessMessage(successMessage);
				}

				if (onSuccess) {
					await onSuccess(result);
				}

				if (autoInvalidate) {
					await invalidateAll();
				}

				await update();
				return;
			}

			// failure
			if (result.type === 'failure') {
				let errorMessage = '';
				let errorMessages: string[] = [];

				if (customErrorHandler) {
					const customResult = customErrorHandler(result);
					if (customResult) {
						errorMessage = customResult.message;
						errorMessages = customResult.messages;
					}
				} else {
					const data = result.data;
					const { validations, message } = data || {};

					if (validations && Array.isArray(validations)) {
						errorMessages = validations;
					} else if (message) {
						errorMessage = message === 'Login failed' ? message + ', please try again' : message;
					} else {
						errorMessage = 'An error occurred. Please try again.';
					}
				}

				if (onError) {
					onError(errorMessage, errorMessages);
				}
			}
		};
	};
};

export const preset = {
	login: (callbacks: Pick<EnhanceOptions, 'onSuccess' | 'onRedirect' | 'onError' | 'onLoading'>) =>
		formEnhance({
			...callbacks,
			logResults: true,
			autoInvalidate: true,
			customErrorHandler: (result) => {
				const { validations, message } = result.data || {};
				if (validations && Array.isArray(validations)) {
					return { message: '', messages: validations };
				}
				if (message === 'Login failed') {
					return { message: 'Login failed, please try again', messages: [] };
				}
				return { message: message || 'Login error occurred', messages: [] };
			}
		}),

	register: (
		callbacks: Pick<EnhanceOptions, 'onSuccess' | 'onRedirect' | 'onError' | 'onLoading'>
	) =>
		formEnhance({
			...callbacks,
			successMessage: 'Account created successfully!',
			customErrorHandler: (result) => {
				const { validations, message } = result.data || {};
				if (validations && Array.isArray(validations)) {
					return { message: '', messages: validations };
				}
				return { message: message || 'Registration failed', messages: [] };
			}
		})
};
