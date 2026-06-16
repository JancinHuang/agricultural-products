const invalidPathValues = new Set(['', null, undefined])

export function requirePathId(value, name = 'id') {
  if (invalidPathValues.has(value)) {
    throw new Error(`${name} 不能为空`)
  }
  return value
}

export function requirePathValue(value, name) {
  if (invalidPathValues.has(value)) {
    throw new Error(`${name} 不能为空`)
  }
  return value
}
