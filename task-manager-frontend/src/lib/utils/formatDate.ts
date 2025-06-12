export function formatToISO(dateTimeLocal: string): string {
	if (!dateTimeLocal) return '';

	const date = new Date(dateTimeLocal);

	const offset = -date.getTimezoneOffset();
	const offsetHours = Math.floor(Math.abs(offset) / 60);
	const offsetMinutes = Math.abs(offset) % 60;
	const offsetSign = offset >= 0 ? '+' : '-';
	const offsetString = `${offsetSign}${offsetHours.toString().padStart(2, '0')}:${offsetMinutes
		.toString()
		.padStart(2, '0')}`;

	return (
		date.getFullYear() +
		'-' +
		(date.getMonth() + 1).toString().padStart(2, '0') +
		'-' +
		date.getDate().toString().padStart(2, '0') +
		'T' +
		date.getHours().toString().padStart(2, '0') +
		':' +
		date.getMinutes().toString().padStart(2, '0') +
		':' +
		date.getSeconds().toString().padStart(2, '0') +
		'.' +
		date.getMilliseconds().toString().padStart(3, '0') +
		offsetString
	);
}
